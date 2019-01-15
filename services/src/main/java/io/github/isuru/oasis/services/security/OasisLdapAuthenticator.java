package io.github.isuru.oasis.services.security;

import io.github.isuru.oasis.services.configs.LdapConfigs;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.Hashtable;

@Component("authLdap")
public class OasisLdapAuthenticator implements OasisAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(OasisLdapAuthenticator.class);

    private final OasisConfigurations oasisConfigurations;

    private DirContext context;
    private Hashtable<String, String> boundEnvMap;

    @Autowired
    public OasisLdapAuthenticator(OasisConfigurations configurations) {
        this.oasisConfigurations = configurations;
    }

    @PostConstruct
    public void init() {
        if (!"ldap".equalsIgnoreCase(oasisConfigurations.getAuth().getType())) {
            return;
        }

        boundEnvMap = new Hashtable<>();
        LdapConfigs ldap = oasisConfigurations.getAuth().getLdap();

        LOG.info("Checking LDAP connection...");
        boundEnvMap.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        boundEnvMap.put(Context.PROVIDER_URL, ldap.getUrl() + "/" + ldap.getBaseDN());

        // To get rid of the PartialResultException when using Active Directory
        boundEnvMap.put(Context.REFERRAL, "follow");

        // Needed for the Bind (User Authorized to Query the LDAP server)
        boundEnvMap.put(Context.SECURITY_AUTHENTICATION, "simple");
        boundEnvMap.put(Context.SECURITY_PRINCIPAL, ldap.getBindDN());
        boundEnvMap.put(Context.SECURITY_CREDENTIALS, ldap.getBindPassword());

        try {
            context = new InitialDirContext(boundEnvMap);
        } catch (NamingException e) {
            LOG.error("Error binding authorized user to the LDAP server {}!", ldap.getUrl());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Copied from
     * https://stackoverflow.com/questions/12163947/ldap-how-to-authenticate-user-with-connection-details/12165647#12165647
     *
     * @param username user name
     * @param password password decrypted.
     * @return true if authentication success, otherwise false.
     */
    @Override
    public boolean authenticate(String username, String password) {
        NamingEnumeration<SearchResult> results = null;
        Hashtable<String, String> env = new Hashtable<>(boundEnvMap);

        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE); // Search Entire Subtree
            controls.setCountLimit(1);   //Sets the maximum number of entries to be returned as a result of the search
            controls.setTimeLimit(5000); // Sets the time limit of these SearchControls in milliseconds

            String searchString = "(&(objectCategory=user)(sAMAccountName=" + username + "))";

            results = context.search("", searchString, controls);

            if (results.hasMore()) {

                SearchResult result = (SearchResult) results.next();
                Attributes attrs = result.getAttributes();
                Attribute dnAttr = attrs.get("distinguishedName");
                String dn = (String) dnAttr.get();

                // User Exists, Validate the Password
                env.put(Context.SECURITY_PRINCIPAL, dn);
                env.put(Context.SECURITY_CREDENTIALS, password);

                new InitialDirContext(env); // Exception will be thrown on Invalid case
                return true;
            }

        } catch (AuthenticationException | NameNotFoundException e) { // Invalid Login
        } catch (SizeLimitExceededException e) {
            LOG.error("LDAP Query Limit Exceeded, adjust the query to bring back less records", username, e);
        } catch (NamingException e) {
            LOG.error("Error authenticating with user {}", username, e);
        } finally {

            if (results != null) {
                try { results.close(); } catch (Exception e) { /* Do Nothing */ }
            }

        }
        return false;
    }

    @Override
    public void close() throws IOException {
        if (context != null) {
            try {
                context.close();
            } catch (NamingException e) {
                throw new IOException("Failed to close ldap bound context!", e);
            }
        }
    }
}

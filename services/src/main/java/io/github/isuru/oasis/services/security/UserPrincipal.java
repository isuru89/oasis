package io.github.isuru.oasis.services.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements UserDetails {
    private Long id;

    private String name;

    private String username;

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String password;

    private int role;
    private boolean active;

    private Collection<? extends GrantedAuthority> authorities;

    @JsonIgnore
    private UserProfile profile;
    @JsonIgnore
    private UserTeam team;

    UserPrincipal(Long id,
                  String name,
                  String username,
                  String email,
                  String password,
                  int role,
                  boolean isActive,
                  Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
        this.active = isActive;
    }

    public static UserPrincipal create(UserProfile user, UserTeam userTeam) {
        int roleId = userTeam.getRoleId();
        List<GrantedAuthority> authorities = new LinkedList<>();

        if (UserRole.hasRole(roleId, UserRole.ADMIN)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN));
        }
        if (UserRole.hasRole(roleId, UserRole.CURATOR)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ROLE_CURATOR));
        }
        authorities.add(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER));

        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getNickName(),
                user.getEmail(),
                user.getPassword(),
                roleId,
                user.isActive(),
                authorities
        );
        principal.team = userTeam;
        principal.profile = user;
        return principal;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public UserTeam getTeam() {
        return team;
    }

    public int getRole() {
        return role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
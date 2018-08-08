openssl genrsa -out private_key.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem -out private.der -nocrypt
openssl rsa -in private.pem -pubout -outform DER -out public.der
rm private.pem
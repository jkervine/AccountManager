# AccountManager 

Requirements:

Mongodb database running in default port on localhost.

Build (and run unit tests) with:

mvn clean package assembly:single 

If you want to build without testing (uses database), use flag -DskipTests=true

Run with:

java -jar target/AccountManager-webapp-jar-with-dependencies.jar

Create new account:

curl -H "Content-Type: application/json" -H "X-Requested-With: XMLHttpRequest" -XPOST http://localhost:8080/account

This returns a new account id string

Query balance:

curl -H "Content-Type: application/json" -H "X-Requested-With: XMLHttpRequest" -XGET http://localhost:8080/account/balance?accountId=[your account id]

Make deposit:

curl -H "Content-Type: application/json" -H "X-Requested-With: XMLHttpRequest" -XPUT http://localhost:8080/account/balance?accountId=[accountId]\&amount=10.0

Make withdrawal:

(same as deposit, but use negative amount)

Inactivate account:

curl -H "Content-Type: application/json" -H "X-Requested-With: XMLHttpRequest" -XDELETE http://localhost:8080/account?accountId=[accountId]



KNOWN issues:

Spring dependecy injection is a workaround since current Jetty+Jersey+Spring combo didn't play along well.

Only Storage class is unit tested

Needs a bit of cleanup still, some leftovers from maven template project






# Events by VeloKofi

Hungry Velos Challenge (Jan 2022):
- Team with max average distance
- Team with max average evelation

Individual awards category (monthly):
- Ms and Mr Alemaari (Max distance)
- Ms Bettamma and Mr Bettappa (Max elevation gain)
- Ms and Mr Minchina Ota (Max average speed)
- Ms Thulimagle and Mr Thulimaga (Max no. of rides)

Hosting details:
- Homepage (Redirects to Strava login and authorization page of Strava): http://31.170.165.24:8080/
- CSV report for all activities: http://31.170.165.24:8080/reports/activities
- CSV report for all activities for a single athlete: http://31.170.165.24:8080/reports/activities/{athleteId}
- Document dump (JSON) for all activities: http://31.170.165.24:8080/documents/activities
- Document dump (JSON) for all activities for a single athlete: http://31.170.165.24:8080/documents/activities/{athleteId}
- Document dump (JSON) for all clients: http://31.170.165.24:8080/documents/clients

Housekeeping (DO NOT USE, UNLESS YOU KNOW WHAT YOU'RE DOING!!!):
- Clear all activities: http://31.170.165.24:8080/documents?action=clearActivities
- Clear all oauth client caches: http://31.170.165.24:8080/documents?action=clearClients
- Clear everything: http://31.170.165.24:8080/documents?action=clearAll
- Cleanup stale data: http://31.170.165.24:8080/documents?action=cleanup

Technical Backlog:
- Theme colors and team colors
- TLS with a domain name
- Single sign-on
- Logging infrastructure with rollover and archiving
- Rate limit compliance for strava data pull
- Continuous Integration and Continuous Deployment (CI/CD) with GitHub Actions: Automation of building, testing, and deployment of the application.
  - On push, trigger build, invoke tests and deploy war file to remote tomcat running on 31.170.165.24 

Software Dependencies:
- mongodb running locally or on docker on port 27017
  - docker run --name mongodb -v mongodata:/data/db -d -p 27017:27017 mongo

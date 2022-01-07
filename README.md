# Events by VeloKofi

Hungry Velos Challenge (Jan 2022):
- Team with max average distance
- Team with max average evelation

Individual awards category (monthly):
- Ms and Mr Alemaari (Max distance)
- Ms Bettamma and Mr Bettappa (Max elevation gain)
- Ms and Mr Minchina Ota (Max average speed)
- Ms Thulimagle and Mr Thulimaga (Max no. of rides)

Technical Backlog:
- Single sign-on
- Logging infrastructure with rollover and archiving
- Responsive charts
- Rate limit compliance for strava data pull
- Continuous Integration and Continuous Deployment (CI/CD) with GitHub Actions: Automation of building, testing, and deployment of the application.
  - On push, trigger build, invoke tests and deploy war file to remote tomcat running on 31.170.165.24 

Software Dependencies:
- mongodb running locally or on docker
  - docker run --name mongodb -v mongodata:/data/db -d -p 27017:27017 mongo

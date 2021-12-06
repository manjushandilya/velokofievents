# Events by VeloKofi

Hungry Velos Challenge (Jan 2022):
- Team with max distance and elevation

Individual awards category (monthly):
- Mr and Ms Alemaari (Max distance)
- Mr and Ms Minchina Ota (Max average speed)
- Bettappa and Bettamma (Max elevation gain)
- Thulimaga and Thulimagle (Max average distance)

Technical Backlog:
- Integrate lombok
- Logging infrastructure with rollover and archiving
- Responsive charts
- Rate limit compliance for strava data pull
- Continuous Integration and Continuous Deployment (CI/CD) with GitHub Actions: Automation of building, testing, and deployment of the application.
  - On push, trigger build, invoke tests and deploy war file to remote tomcat running on 31.170.165.24 

Software Dependencies:
  - docker run --name mongodb -v mongodata:/data/db -d -p 27017:27017 mongo

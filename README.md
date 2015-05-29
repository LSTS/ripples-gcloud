# Ripples
This project provides situation awareness of a networked vehicle system deployment through a simple web interface.

# Data Sources
* FindMeSpot (for SPOT satellite trackers / markers)
* ARGOS Web Service (for Argos markers)
* RockBlock (for pushed Iridium messages)
* FireBase (for real-time position updates)

# Servlets
* Systems Servlet
  * Accessible from **/api/v1/systems/**
  * Used to store the latest known position for each system
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/SystemsServlet.java
* RockBlock Servlet
  * Accessible from **/rock7/**
  * Used to parse incoming iridium messages (posted by RockBlock Iridium Provider)
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/Rock7Servlet.java
* Spot Updater Servlet
  * Accessible from **/spots/**
  * Used to poll periodically the FindMeSpot service in order to fetch new Spot drifter positions
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/UpdateSpotsServlet.java
* Addresses Updater Servlet
  * Accessible from **/addresses/**
  * Used to poll periodically the IMC addresses from IMC's github repository.
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/AddressesServlet.java
* Argos Updater Servlet
  * Accessible from **/argos/**
  * Used to poll periodically the Argos web service in order to fetch new Argos drifter positions
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/ArgosServlet.java
* Firebase Updated Servlet
  * Accessible from **/fbase/**
  * Used to poll the Firebase API in order to fetch new system positions
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/UpdateFirebaseServlet.java
* Iridium Servlet
  * Accessible from **/api/v1/iridium/**
  * Used to post messages to known Iridium destinations and poll messages historical data
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/IridiumServlet.java
* Positions Servlet
  * Accessible from **/positions/**
  * Used to access historical position data
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/PositionsServlet.java
* LogBook Servlet
  * Accessible from **/logbook/**
  * Logbook backend
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/LogbookServlet.java
* CSV Positions servlet
  * Accessible from **/api/v1/csvTag/**
  * Servlet that generates CSV data from historical position information
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/PositionsCsvServlet.java
* Points of Interest Servlet
  * Accessible from **/poi/**
  * Servlet that is used to store/access POIs provided by users
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/PoiServlet.java
* Iridium Updater Servlet
  * Accessible from **/iridiumUpdates/**
  * Servlet that sends iridium updates to subscribed devices
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/IridiumUpdatesServlet.java
* Raia Buoys Servlet
  * Accessible from **/raia/**
  * Backend for RAIA buoys state
  * https://github.com/LSTS/ripples/blob/master/src/pt/lsts/ripples/servlets/RaiaServlet.java

  
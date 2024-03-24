-- CREATE TABLE trips (
--     tripID INTEGER PRIMARY KEY AUTOINCREMENT,
--     userID INTEGER, -- If you associate trips with users
--     startTime DATETIME NOT NULL,  
--     endTime DATETIME, -- Can be NULL initially
--     autoDetected BOOLEAN -- Optional - if the trip was detected automatically
-- ); 

-- CREATE TABLE trip_segments (
--     id INTEGER PRIMARY KEY AUTOINCREMENT,
--     tripID INTEGER NOT NULL, 
--     distance DOUBLE NOT NULL,   
--     duration INTEGER NOT NULL, 
--     FOREIGN KEY (tripID) REFERENCES trips(tripID)
-- );

CREATE TABLE trip_metrics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tripID INTEGER NOT NULL, 
    averageSpeed DOUBLE,   
    topSpeed DOUBLE,
    tripLength INTEGER,  -- Total trip duration in seconds
    tripDistance DOUBLE, 
    speedingInstances INTEGER,
    hardBrakingInstances INTEGER,
    rapidAccelerationInstances INTEGER,  -- Add this new column
    FOREIGN KEY (tripID) REFERENCES trips(tripID)
);
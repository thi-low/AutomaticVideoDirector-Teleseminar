Project Overview
====
This is a re-upload of a university project from 2014 !!!!!!!

# Possible Description
Nowadays, smartphones are widely used for video capture. A disadvantage of these small devices compared to camcorders is the large amount of shaking in captured video. Several video stabilization algorithms have been proposed to improve the quality of shaky videos. These algorithms typically require the analysis of the pixels in each video frame and to track the objects’ positions over time. The computational effort of object tracking is very high and not applicable in smartphones.
Smartphones may offer additional information about the captured video. Motion sensors can be used to collect data about the orientation and viewing direction. The task is to analyze the shakiness of a video and compare it to motion sensor data. The students should evaluate if motion sensor data can be used to identify problematic video segments and also apply shake-reduction algorithms using this data to stabilize them.


# Implemented Features

1. Android application which extracts motion sensor data captured by a smartphone to analyze the quality of a video.

2. Implements the data exchange between a server and several phones. The phones compute the video quality from sensor information and send it to the server. 

3. The server makes a decision about suitable video streams and sends notifications to the clients.

4. Notified clients upload their videos to the server.

Credits go to the team: 
	- https://github.com/egurnov
	- https://github.com/fwsGonzo
	- https://github.com/jonup
	- https://github.com/thi-low

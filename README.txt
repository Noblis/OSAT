Introduction
OSAT (the Open Source Accessibility Toolkit) provides a set of tools, to be used in conjunction with OpenStreetMaps and various open data sources, for analyzing transportation accessibility (access to destinations) and for creating visualizations of the results.  

The ability to reach desired goods, services, activities, and destinations is an important performance metric for transportation systems.  Accessibility is a mode neutral, user-focused, quantitative metric that allows alternatives to be evaluated on an equal footing.  Accessibility metrics place the focus on the end goal of the transportation system, which is rarely to provide travel for it’s own sake, but rather to meet a need.   Accessibility metrics are a powerful complement to other mobility metrics such as average travel speed or delay.

The metrics that can be calculated include opportunity and gravity model based accessibility measures, as well as estimates of the modal accessibility gap.  Regional, normalized transportation-focused metrics, such as the average percentage of jobs reachable within a given amount of travel time can be calculated.  The goal of this new approach is to allow more groups, with some level of software expertise, to conduct accessibility studies using free publicly available data and without expensive software.  This opens up such analysis to a much broader array of stakeholders.

Tools and methods	
Open data are data sets that are available for reuse by third parties with few or no restrictions, readily available at little or no cost, easy to find, and coded using standard, open formats.  For example, for our proof of concept work, the following open data sources were used:
Transportation Analysis Zone (TAZ) boundaries – U.S. Census Bureau (we have also used Census Block Group data)
 Demographic data (job, household, and population data) – U.S. Census Bureau
 Transit schedule information -   various transit agencies serving Washington, D.C. and King County, Washington.
 Map information - U.S. Census Bureau TIGER files and the OpenStreetMaps project.  

Open source software used in the project ranged from development tools through the OpenTripPlanner application.  Open softtware used in the project included:
Development tool chain –  Java, Python, Eclipse
GIS tools – Quantum GIS, MapWindows GIS, Shape2Earth plug-in 
Door to door transit trip planning: OpenTripPlanner

The repository includes source code, data sets, and sample output visualizations.  The visualization tool could be used separately if the user has a different approach for analyzing accessibility rather than using OpenTripPlanner.  It simply requires that the necessary information be put into apppropriately formatted XML data files. 

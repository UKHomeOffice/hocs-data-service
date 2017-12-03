
# Hocs Data Service

 - See also [kube-hocs-data-service](https://github.com/UKHomeOffice/kube-hocs-data-service).
 - See also [hocs-data](https://github.com/UKHomeOffice/hocs-data) (private repo)

hocs-data-service is part of a larger series of work to replace functionality currently in [hocs-alfresco](https://github.com/UKHomeOffice/hocs-alfresco).
It serves a set infrequently-updated data specific to hocs although any individual data type is usable in isolation.
This data was previously stored in an Alfresco instance but storing it as relational data is much more appropriate as it allows better management and easier querying.
 It should be noted that this solution meets the immediate needs of the Hocs project and was written under time pressure. There is plenty of scope to improve this service in the future.

## Topics
A Hocs case has one or more topics associated with it. Topics are stored against a list name. The list name is typically the a Hocs business unit name e.g. 'DCU', 'UKVI'.
Topics are hierarchical lists of data like so:

    - Topic Group
    -- Topic Item 1
    -- Topic Item 2
    - Gun Crime
    -- Shotguns 
    -- Hand Guns

Topic items also hold data about the Hocs Units and Teams the topic relates to. 
Currently we receive two CSV files from the DCU and UKVI Units which are checked into the hocs-data project. 

We have POST and PUT on the `'/topics/{unitName}'` path and a GET on `'/topics/topicList'` as the lists are only accessible as a combined list at the moment.

## User Accounts
User accounts are mastered in the hocs-data-service but are also required to be created in hocs-alfresco. Several CSV files are checked in to the hocs-data project. 

We have POST and PUT on the `'/users/{unit}'` where unit is a business unit supported by the hocs project e.g. 'UKVI'.
To put the data in the Alfresco we also have a GET on `'/users/{unit}/export'` which returns a JSON body for use in the alfresco webscript `/alfresco/s/importUsersAndGroups/` which needs to be applied manually. 

### Future development
Unfortunately this webscript restricts the number of users that can be processed at once, this can be problematic. Having hocs-data-service manage adding/removing these itself in small batches would be much better, however removing Alfresco is also on the project roadmap.

## Units/Teams
Units and Teams are a representation of Home Office business units and team names e.g.

    - Crime, Policing and Fire Group Crime Directorate	
    -- Drugs & Firearms Licensing Unit
    -- Crime and Safeguarding Delivery Unit
    -- Matters for Major Events Hub
    - FOI
    -- Information Rights Team

Units and teams are mastered in the hocs-data-service but are also required to be created in hocs-alfresco for the BPMN engine, a CSV file is checked in to the hocs-data project.

We have POST and PUT on the `'/groups'` path and a GET on `'/groups'`
To put the data in the Alfresco BPMN engine we also have a GET on `'/groups/export'` which returns a JSON body for use in the alfresco webscript `/homeoffice/ctsv2/manageGroups/` which needs to be applied manually.

The Units/Teams added to hocs-data-service are given a reference name for use in Alfresco in lieu of an ID e.g.
`Crime, Policing and Fire Group Crime Directorate` might become `GROUP_CPFCD`. This has a limit of 100 including the GROUP_ added by Alfresco.

### Future development
Having hocs-data-service manage adding/removing these to Alfresco itself would be much better, however removing Alfresco is also on the project roadmap.
 
## Parliamentary Members (WIP)
Parliamentary members are members from several parliaments and assemblies (Commons, Lords, EU, Welsh, Scottish, Northern Irish). All but the Welsh assembly are updated on demand using online data feeds, the Welsh Assembly details are checked in to the hocs-data project.

## Simple Key Value Pairs
Hocs-data-service has the ability to serve simple KVP data (like that used in a drop down list). These are addressable as named lists.

We have POST and PUT on the `'/list/{name}'` path and a GET on `'/list/{name}'`

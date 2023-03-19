Polish :

- use log file as described in the manifest file
- some versions need special assets dir in order to work :
	- "legacy" (~1.6) assets/virtual/legacy/ where assets are normal files
	- "pre-1.6" (<1.6) game dir/resources/ where assets are normal files
- actually assets are only downloaded if the assets index doesn't exist
- if a game instance hasn't started after a second is lhaunched, it will crash because the second time an instance is launched it checks if the first is alive

Features :

- handle Fabric manifest (inheritsFrom, ...)
	- MAYBE install fabric version
	- MAYBE handle Forge manifest (inheritsFrom, ...)
- Microsoft connection
	- save tokens
		- add a button to reconnect with tokens
- buttons to order lists of profiles by last played time or alphabeticaly
- filter profiles by version type : [x] Releases [ ] Snapshot [ ] Historical [x] Modded
- display download percentage and amont (assetIndex.totalSize)

Runtime :

- download and use local runtimes (https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json)

QoL :

- thread download of "manifest_version"
- enter key launch instance
- order versions by time
- buttons to reset game dir, resolution, exe, jvm

Design :

- Improve design by adding CSS
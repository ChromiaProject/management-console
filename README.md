# Management Console

The management console is a cli for interacting with the directory chain (management chain) on a chromia network.

## Build

This project is developed together with the directory chain. It will download the rell code from that repo. 
If you want to use the source code directly from your host, use the `-Dlocal` flag. You can also explicitly set the path to where the source code is using `-Ddirectory.source=<path>` flag.

```shell
$ mvn package  // Build and package this project
$ mvn package -Dlocal // Uses directory chain code from ../directory-chain/src
$ mvn package -Dlocal -Ddirectory.source=/path/to/directory-chain/src
$ mvn package -Pdocker // Also builds a docker image `pmc` 
$ mvn package -Pdocker-push // Also builds a multi-arch docker image and pushes it to configured docker registry
```

## Usage

Configuration of `pmc` can be done one three levels:

```shell
~/.pmc/config # global configuration file
.pmc/config   # local configuration file
env CHROMIA_CONFIG # overriding file
```

Configuration can be done using for example `pmc config --global/--local --set api.url=<url>`

| Property        | Definition                                           | Comment                                                                   |
|-----------------|------------------------------------------------------|---------------------------------------------------------------------------|
| api.url         | Api url of a node in the chromia network             | Can be a comma-delimited list                                             |
| brid            | Blockchain rid of the directory chain                |                                                                           |
| pubkey          | Public key                                           |                                                                           |
| privkey         | Private Key                                          | Used to sign any transactions sent to the network                         |
| provider.pubkey | The default provider to be set for provider commands | This is by default looked up automatically, but can be set to be skipped. |

Note: The public/private keypair is only needed if you intend to send operations

## Compatibility

The tool is built to be backwards compatible with at least the publicly deployed networks. It is thus important that this repo is upgraded whenever the directory chain has a new minor release.

## Releases

After dev branch has been built in the CI, open up the pipeline and start either `release-patch` or `release-minor` stage to create a release.

Whenever a new command is added, removed or has changed behavior to the user, a minor release can be created. Any bugs or minor aesthetics that is useful to have for the user can be a patch release.

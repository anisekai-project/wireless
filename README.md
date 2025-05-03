# Anisekai Wireless

This package is used in every anisekai java project, as it contains everything needed to connect different services and also utils.

This also includes a whole custom library for discord interaction, which is a rewrite of my jda-interactions-extension project (that I will
surely deprecate one day in favor of this project anyway)

### Packages

*Note: Not all packages are listed there, only the most important ones.*

- `fr.anisekai.wireless.api`: All the tools that can work independently of each others. Most of the time API / ABI or other tools. Mostly
  not tied to the Anisekai project altogether.
- `fr.anisekai.wireless.interfaces`: All interfaces used in the wireless project, mostly functional interfaces that can be used for many
  things.
- `fr.anisekai.wireless.remote`: Core of the Anisekai project, this will contain all shared definition across every java project.

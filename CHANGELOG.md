# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Changed

- Forked MastodonC-fork of the original ymilky projects (cause they had some additional fixes). Structural change to projects to use a monorepo.


## [0.0.2] - 2016-03-12

### Added

- Parsers for connection data/connection strings
- Cluster Metadata support and associated functionality
- Helpers for working with topic partitions
- Ability to create a partition destination to test/develop or for special use-cases for partitioners
- Tweaked schema slightly for better performance and got rid of some calls to deprecated schema functions
- Dependency updates for schema

### Fixed

- Fixed decode/encode flipped in one of the artities for consumer constructor



## [0.0.1] - 2016-03-11
### Added

- Initial Release

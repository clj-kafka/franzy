# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Changed 

- Updates documentation

[Git Log](https://github.com/clj-kafka/franzy/compare/v2.0.6...HEAD)

## [v2.0.6] - 2017-09-13

### Changed
- Forked MastodonC-fork of the original ymilky projects (cause they had some additional fixes). Structural change to projects to use a monorepo.
- migrated to Kafka 0.11.0.0 (should be also okay for 0.10.2.x, but has not been tested)



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

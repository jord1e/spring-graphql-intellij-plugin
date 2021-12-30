# Spring GraphQL Support

## [Unreleased]

### Added

## [0.0.2-alpha.5]

### Fixed

- IntelliJ IDEA `2021.3.1` compatibility

## [0.0.2-alpha.4]

### Added

- Navigation between annotated mapping methods and the schema
- Autocompletion in mapping annotations for types and fields (works for `@SchemaMapping` and `@BatchMapping`)
- Autocompletion for `@Argument` names associated with the field
- Automatic filling of `typeName` when autocompleting fields
- Return type checks for `@BatchMapping`
- Data fetchers in the project window
- Support for `@SchemaMapping(typeName = "...")` on classes
- Inspection for adding required `@Controller` annotations
- Inspection for `@SchemaMapping` and `@BatchMapping` on the same method

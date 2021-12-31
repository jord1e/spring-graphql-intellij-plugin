# Spring GraphQL Support

## [Unreleased]

### Added

- GraphQL injection for `GraphQlTester#query(String)`

### Fixed

- IntelliJ IDEA `2021.3.1` compatibility
- Correct display of Spring GraphQL data fetchers, and batch loaders in project window
- Intention for adding `@Controller` does not highlight the entire class anymore
- Autocompletion on `@BatchMapping` fields now changes the `typeName`

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

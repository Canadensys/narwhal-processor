Version History
===============

Version 1.5.3 2015-05-22
 * Introduced new generic DictionaryBased processor
 * New experimental PersonNameProcessor

Version 1.5.2 2015-01-23

[Details](https://github.com/Canadensys/narwhal-processor/milestones/1.5.2)

Version 1.5.1 2014-01-24
* Fix issue that may prevent date interval processing

Version 1.5 2013-12-09
* Added CoordinatesToWGS84Processor to convert coordinates using GeoTools 10

Version 1.4 2013-10-10
* Added date interval parsing
* Added province abbreviations for Canada
* Added travis-ci

Version 1.3 2013-07-11
* Now using gbif-parsers 0.4
* Fixed issue where 180.1 degrees was considered valid

Version 1.2 2013-04-15
* Now supports full month (non abbreviated) in date parsing
* Initial Locale support to support date in French and Spanish
* The month part of a date parsing is now case insensitive

Version 1.1 2013-04-05
* DataProcessor interface is now AbstractDataProcessor abstract class
* DateProcessor now returns Integer[] in process(...) function to match other processors implementation
* new class StateProvinceNameParser created, used by StateProvinceProcessor
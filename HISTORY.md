Version History
===============

Version 1.5 TBA
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
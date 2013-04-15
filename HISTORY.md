Version History
===============

Version 1.2 2013-04-15
* Now supports full month (non abbreviated) in date parsing
* Initial Locale support to support date in French and Spanish
* The month part of a date parsing is now case insensitive

Version 1.1 2013-04-05
* DataProcessor interface is now AbstractDataProcessor abstract class
* DateProcessor now returns Integer[] in process(...) function to match other processors implementation
* new class StateProvinceNameParser created, used by StateProvinceProcessor
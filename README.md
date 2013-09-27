JavaLang
========

Java Language support


Building for eclipse
====================
1. Download Java-Lang zip from git 
    https://github.com/OpenHFT/Java-Lang/archive/master.zip
2. Unzip master.zip, Java-Lang-master folder will be extracted from zip.
3. cd Java-Lang-master
4. mvn eclipse:eclipse
5. Now you have an eclipse project, import project into Eclipse
6. If your Eclipse configuration is not UTF-8, after importing the project you may see some errors and strange characters in some .java files. To get rid of this problem change character enconding to UTF-8:
   project->properties->resource->text file encoding->utf8

# a script example to build Java project 

project =aldan3-jdo
"build_directory" = ./build
source_directory ="src/java"
doc_directory=doc
build_file ="${project}.jar"
 domain ="org"
resources ="${domain}.${project}.resources"
manifestf =""
main_class= "${domain}.${project}.Main"
test class=""

comm =..${~/~}simscript${~/~}comm-java.7b:file

include(comm)
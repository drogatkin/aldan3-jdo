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

target clean {
    dependency {true}
    exec rm  (
        -r,
        ${build_directory}/${domain},
        ${build_directory}/${build_file}
    )
}

target compile:. {
   dependency {
       or {
              newerthan(${source_directory}/.java,${build_directory}/.class)
       }
   }
   {
        display(Compiling Java src ...)
       newerthan(${source_directory}/.java,${build_directory}/.class)
       assign(main src,~~)
       exec javac (
         -d,
         ${build_directory},
        -cp,
         ${build_directory},
         -source,
         8,
         -target,
         8,
         main src
       )     
      if {
         neq(${~~}, 0)
         then {
            panic("Compilation error(s)")
         }
     }
   }
}

target jar {
      dependency {
         anynewer(${build_directory}/${domain}/*,${build_directory}/${build_file})
      }
      dependency {
          target(compile)
      }
     
     {    display(Jarring ${build_file} ...)
          exec jar (
            -cf,
            ${build_directory}/${build_file},
            -C,
            ${build_directory},
            ${domain}
          )
     }
}
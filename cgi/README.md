Visualizador de objetos 3D
=====
Trabalho realizado no âmbito da cadeira de Computação Gráfica e Interfaces (2014/15)

Funcionalidades:

- Carregamento de modelos 3D no formato [obj](http://en.wikipedia.org/wiki/Wavefront_.obj_file);
- Visualização do objeto em diferentes modos (wireframe, solifd fill ou solid&wireframe);
- Escolha entre uma única projeção ou projeções multipas;
- Manipulação dos valores dos parâmetros das diferentes projeções;
- Carregamento de imagens (bitmaps) a usar como texturas aplicadas aos polígonos do objeto a ser visualizado.

Execução:

Correr o seguinte comando na pasta raiz do programa:

(UNIX)
java -Djava.library.path="aux_libs/versao_do_OS/" -cp bin:aux_libs/jogl-all.jar:aux_libs/gluegen-rt.jar Objectos3D

(WINDOWS)
java -Djava.library.path="aux_libs/versao_do_OS/" -cp bin;aux_libs/jogl-all.jar;aux_libs/gluegen-rt.jar Objectos3D

onde versao_do_OS é uma das pastas localizada em aux_libs, de acordo com a versão do Sistema Operativo

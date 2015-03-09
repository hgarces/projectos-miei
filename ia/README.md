Genetic Algorithm
=====
Trabalho realizado no âmbito da cadeira de Inteligência Artificial (2014/15)

Este trabalho consiste na implementação de um algoritmo de procura local para a resolução de uma variante do problema do caixeiro-viajante, isto é feito recorrendo a um [algoritmo genético](http://en.wikipedia.org/wiki/Genetic_algorithm) para obter uma solução que aproxima a solução óptima.

Funcionalidades:

+ Possibilidade de definir o modo de funcionamento do algoritmo, nomeadamente: 
  i. método de seleção; 
  ii. operadores de recombinação e mutação; 
  iii. critério de paragem;
  iv. forma de determinação da população inicial 

Execução:

admitindo execução a partir do interior da pasta bin
```sh
(UNIX)
export CLASSPATH=.:../aux_libs/jcommon-1.0.23.jar:../aux_libs/jfreechart-1.0.19.jar

(WINDOWS)
set classpath=%classpath;.;../aux_libs/jcommon-1.0.23.jar;../aux_libs/jfreechart-1.0.19.jar
```
```sh
java circuit.CircuitTest
```

##PASSO 1 AO 3 -DANIELA ARANHA

Em relacao ao jogo service sinto que poderia concentrar menos responsabilidades dentre elas:
UI do jogo e a entrada ,geracao e aleatorizacao do mapa, as regras do jogo(colisao,embarque e posicao)
possiveis melhorias: remover o gerador de missao  e entrada, deixando o jogoservice somente pra orquestrar o loop do jogo e as regras

outro ponto e que o mapa renderer e instanciado diretamente com new MapaRender() dentro do construtor ao inves de ser injetado, o ideal deveria ser criar uma interface e injetar via construtor, assim como ja feito com o repositorio

posicionar passageiros decide o tipo de passageiro com um if/else, que precisaria ser editado a toda vez que um novo tipo de passageiro for adicionado(OCP)
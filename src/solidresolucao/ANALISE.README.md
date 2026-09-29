##PASSO 1 AO 3 -DANIELA ARANHA

Em relacao ao jogo service sinto que poderia concentrar menos responsabilidades dentre elas:
UI do jogo e a entrada, geracao e aleatorizacao do mapa, as regras do jogo(colisao, embarque e posicao)
possiveis melhorias: remover o gerador de missao e entrada, deixando o jogoservice somente pra orquestrar o loop do jogo e as regras

outro ponto e que o mapa renderer e instanciado diretamente com new MapaRender() dentro do construtor ao inves de ser injetado, o ideal deveria ser criar uma interface e injetar via construtor, assim como ja feito com o repositorio

posicionar passageiros decide o tipo de passageiro com um if/else, que precisaria ser editado a toda vez que um novo tipo de passageiro for adicionado(OCP)

Passo 4 - Arthur Adcleybson

Qual seria o impacto se o jogo dependesse diretamente de um arquivo em vez de uma interface?

Se o JogoService usasse o RankingService direto, toda vez que eu quisesse mudar a forma de salvar o ranking (por exemplo, trocar o arquivo por um banco de dados) eu teria que mexer no JogoService também. Ele ficaria preso a um detalhe que não tem a ver com a regra do jogo. Com a interface, eu consegui até testar usando outro RankingRepository com um arquivo temporário.

Por que a abstração do ranking aumenta a flexibilidade do sistema?

Porque a interface define o que precisa ser feito (salvar, listar, limpar) e não como é feito. Assim, qualquer classe que implemente RankingRepository pode substituir o RankingService sem o JogoService perceber. Isso ajuda a trocar o armazenamento depois e também a fazer testes.

Como isso exemplifica a inversão de dependência do SOLID?

O JogoService (alto nível) e o RankingService (baixo nível) dependem da interface RankingRepository, e não um do outro. Quem escolhe a implementação é a Main, quando cria o objeto e passa pro construtor do JogoService.

Passo 5

Como a herança de Passageiro permite a criação de novos tipos sem mudar a lógica principal?

Porque o resto do sistema só usa o que a classe Passageiro define (nome, tipo, posição, getPontuacao() e getSimbolo()). Se eu quiser criar um Cientista, é só estender Passageiro e implementar os dois métodos, sem mexer em Missao, MapaRenderer ou JogoService. A única exceção é o trecho que sorteia o tipo em posicionarPassageiros, que ainda tem uma lista fixa.

Por que a interface Movel é melhor do que criar métodos específicos em cada classe?

Porque assim Nave e Inimigo têm o mesmo método mover(dx, dy) e dá pra tratar os dois do mesmo jeito. Sem a interface, eu teria que colocar mover() até no Asteroide, que não se move, ou usar instanceof pelo código. Só quem se move implementa a interface, o que segue o ISP.

Qual parte do modelo mostra melhor o princípio de substituição de Liskov?

As subclasses de Passageiro (Professor, Engenheiro e Astronauta). Elas só sobrescrevem getPontuacao() e getSimbolo(), e o código nunca precisa saber qual é a subclasse antes de usar. Testei isso no TesteRanking.java somando a pontuação das três tratadas como Passageiro, sem cast.

Perguntas gerais

Qual princípio do SOLID você achou mais importante nesta atividade?

O DIP, no ranking. Com ele consegui testar o RankingService separado, sem rodar o jogo inteiro pelo console.

Em que parte da refatoração você percebeu melhor a diferença entre um código acoplado e um código mais organizado?

Na separação do MapaRenderer da regra do jogo. Antes, desenhar o mapa, calcular pontos e verificar colisão ficavam tudo junto. Depois, o MapaRenderer só transforma o estado da Missao em texto. Quando troquei a forma de pegar o símbolo do passageiro (para getSimbolo()), só precisei mexer nele.

O que você aprendeu sobre manutenção e evolução do software?

Que separar responsabilidades diminui o quanto uma mudança se espalha pelo código. Trocar o arquivo de ranking por um banco, por exemplo, seria só criar uma nova classe que implementa RankingRepository. Também vi que mesmo um código pensado com SOLID pode ter erros (como o símbolo do asteroide não bater com a legenda), então ainda é preciso testar e revisar.

Se fosse adicionar uma nova funcionalidade, qual parte do código você alteraria com mais segurança?

O pacote model, criando uma nova subclasse de Passageiro (como um Cientista que vale mais pontos). É só criar um arquivo novo e implementar dois métodos, sem quebrar Missao, MapaRenderer ou JogoService.
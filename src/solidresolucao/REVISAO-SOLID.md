# Revisão final da atividade

Nome: Arthur Adcleybson
Data: 28/09/2026

## Como validei a solução

Registre os comandos executados e os fluxos testados:

- [x] compilação do código inicial (`src/exercicio10`) — `javac -d out src/exercicio10/*.java`, sem erros.
- [x] compilação da versão refatorada (`src/solidresolucao`) — sem erros e sem warnings (`javac -Xlint:all`).
- [x] início de uma missão — menu → dificuldade → tamanho do mapa → mapa desenhado corretamente.
- [x] movimentação, embarque e conclusão da missão — comandos `w/a/s/d` respeitando os limites do mapa, comando `c` embarcando passageiro na posição da nave (ou avisando "nave cheia"/"nenhum passageiro"), comando `q` abortando a missão.
- [x] colisão — colisão com inimigo detectada, vida decrementada, `GAME OVER` ao zerar vidas.
- [x] consulta e reset do ranking — ranking vazio exibe aviso; opção 3 apaga o arquivo e confirma a remoção.
- [x] outro teste: criei um pequeno programa avulso (não faz parte da entrega, só de apoio durante o desenvolvimento) que instancia `RankingService`, `Missao` e as subclasses de `Passageiro` diretamente, sem passar pelo console, para conferir salvar/listar/limpar do ranking, embarque, colisão e o cálculo polimórfico de pontuação. Todos os casos passaram.
- [ ] fluxo completo de vitória (todos os passageiros embarcados + retorno a (0,0) + gravação no ranking): validado manualmente jogando uma partida em um mapa pequeno (tamanho 2), pois a posição aleatória dos passageiros dificulta automatizar esse cenário sem alterar a geração da missão.

## Achados da revisão

### SRP

```text
Local: solidresolucao.service.JogoService (classe inteira, principalmente jogarPartida)
Princípio relacionado: SRP
Observação: mesmo depois de separar apresentação (MapaRenderer) e persistência
(RankingRepository), o JogoService ainda concentra três coisas: ler a entrada
do jogador no console, decidir as regras de uma rodada (pontuação, dificuldade,
condição de vitória) e sortear as posições iniciais de passageiros/asteroides/
inimigos.
Impacto: para testar a regra "perder 1 ponto a cada movimento", por exemplo,
ainda é preciso simular um Scanner inteiro. A classe também cresce toda vez que
qualquer uma dessas três coisas muda.
Proposta: extrair um leitor de entrada (algo como EntradaConsole) e um
gerador de missão (GeradorDeMissao) como colaboradores injetados no
JogoService, deixando-o só orquestrar a sequência de eventos da partida.
Prioridade: média
```

### OCP

```text
Local: solidresolucao.service.JogoService.posicionarPassageiros
Princípio relacionado: OCP
Observação: a escolha de qual tipo de passageiro é criado a cada posição
(indice % 3 → Professor, Engenheiro ou Professor de novo) está escrita direto
no método. Para acrescentar um novo tipo de passageiro (por exemplo, um
"Cientista") ainda é preciso editar esse trecho do serviço.
Impacto: mistura duas decisões diferentes — "quantos passageiros colocar" e
"quais tipos existem" — no mesmo lugar. Isso reduz o ganho de ter criado a
hierarquia de Passageiro pensando em extensão.
Proposta: mover a lista de "tipos disponíveis" para fora do serviço, por
exemplo uma lista de fábricas (List<PassageiroFactory> ou um enum
TipoPassageiro com um método criar(nome, x, y)) injetada no JogoService. Um
novo tipo passaria a ser adicionado nessa lista, sem tocar no laço que já
funciona.
Prioridade: média
```

### LSP

```text
Local: solidresolucao.model.Passageiro e as subclasses Professor, Engenheiro,
Astronauta
Princípio relacionado: LSP
Observação: as três subclasses só sobrescrevem getPontuacao() e getSimbolo();
nenhuma delas lança exceção nova, restringe o intervalo de x/y ou muda o
significado de getNome()/getTipo(). Em nenhum lugar do código (embarque,
cálculo de pontuação, renderização do mapa) foi preciso usar instanceof para
tratar um tipo de forma diferente do que o contrato de Passageiro promete.
Impacto (positivo): confirma que qualquer subclasse pode substituir
Passageiro sem surpreender quem já usa a classe base — é justamente essa
garantia que permite ao OCP funcionar na prática.
Proposta: manter a regra como critério para futuras subclasses: uma nova
categoria de passageiro só deve sobrescrever os dois métodos abstratos e não
deve impor pré-condições mais restritivas nem lançar exceções que a classe
base não previa.
Prioridade: baixa (nenhum problema encontrado; item de vigilância para o futuro)
```

### ISP

```text
Local: solidresolucao.repository.RankingRepository
Princípio relacionado: ISP
Observação: a interface tem quatro operações (duas variações de salvar,
listar e limpar) e o único cliente hoje é o JogoService, que usa as quatro.
Não encontrei nenhum método "órfão" que uma implementação seja obrigada a
escrever sem que ninguém o chame.
Impacto: a interface está do tamanho certo para o projeto atual; criar uma
implementação alternativa (por exemplo, um RankingEmMemoria para testes) não
exige implementar nada supérfluo.
Proposta: nenhuma mudança agora. Se no futuro surgir uma tela que só
precisa listar o ranking (sem nunca salvar ou limpar), aí sim valeria separar
leitura e escrita em duas interfaces menores; fazer isso hoje seria
complexidade sem benefício imediato.
Prioridade: baixa
```

### DIP

```text
Local: solidresolucao.service.JogoService (construtor)
Princípio relacionado: DIP
Observação: o serviço depende corretamente da abstração RankingRepository
(recebida no construtor), e Main é o único ponto que conhece a implementação
concreta RankingService. Porém, dentro do próprio construtor de JogoService,
o Random e o MapaRenderer ainda são criados com "new" diretamente
(this.random = new Random(); this.mapaRenderer = new MapaRenderer();).
Impacto: isso torna impossível controlar a semente do gerador aleatório em um
teste automatizado (o sorteio de posições nunca é determinístico) e impede
trocar a apresentação por outra implementação sem alterar o construtor do
serviço.
Proposta: receber Random e MapaRenderer também via construtor (ou por uma
interface de renderização), deixando o JogoService livre de qualquer "new" de
dependência.
Prioridade: alta — é o ponto que mais atrapalha testes automatizados hoje.
```

## Decisões com as quais concordo

Concordo com a decisão de extrair `RankingRepository` como interface e deixar
`RankingService` como a única implementação concreta que conhece arquivo em
disco (`Passo 4` do tutorial). O benefício apareceu na prática: para escrever
o programa de apoio que usei nos testes manuais, bastou apontar
`RankingService` para um arquivo temporário (`/tmp/ranking-teste.txt`) sem
precisar mexer em `JogoService`. Se um dia o ranking precisar ir para um banco
de dados ou uma API, o mesmo raciocínio se aplica: cria-se uma nova classe que
implementa `RankingRepository` e só se troca a linha de composição em `Main`.

## Decisões com as quais não concordo

Não concordo com a decisão de fazer a capacidade da nave (`Nave`) ser sempre
igual à quantidade total de passageiros da dificuldade escolhida
(`new Nave("A-1", 0, 0, qtdPassageiros)`, no `JogoService`). Na prática isso
faz a mensagem "Nave cheia! Não há espaço para mais passageiros." nunca
aparecer, porque a nave sempre tem vaga para todos. No jogo original, a
capacidade era fixa (5) independentemente da dificuldade, o que criava uma
decisão real para o jogador: em qual ordem embarcar e quando arriscar voltar à
plataforma antes de encher a nave. Prefiro manter uma capacidade fixa e
menor que o total de passageiros nas dificuldades mais difíceis — isso não
muda nenhuma classe nem estrutura, é só um parâmetro diferente na composição
da missão, mas devolve um pouco do desafio original sem aumentar a
complexidade do código.

## Melhoria implementada (opcional)

Duas pequenas melhorias já foram implementadas em relação ao tutorial de
referência, e não apenas identificadas na revisão:

1. **`MapaRenderer` sem `instanceof`/comparação de string por tipo.** O
   tutorial original comparava `passageiro.getTipo().equals("Engenheiro")`
   dentro da apresentação para escolher o símbolo. Troquei por
   `passageiro.getSimbolo()`, que cada subclasse de `Passageiro` já expõe
   (herdado de `EntidadeMapa`). Assim, um novo tipo de passageiro passa a
   aparecer no mapa automaticamente, sem precisar alterar `MapaRenderer` —
   um exemplo mais completo de OCP, já que antes a apresentação precisaria
   ganhar um novo `else if` para cada tipo novo.
2. **Métodos menores em `JogoService.jogarPartida`.** Extraí
   `embarcarPassageiro(Missao)` e `verificarConclusaoDaMissao(...)` do corpo
   do laço principal, que no tutorial ficava com bastante lógica aninhada em
   um único método. O comportamento é idêntico; a diferença é só
   legibilidade e testabilidade unitária de cada trecho.

A melhoria de maior prioridade (extrair a leitura de `Random` do
`JogoService` para permitir testes determinísticos) fica registrada acima
como proposta para uma próxima iteração.

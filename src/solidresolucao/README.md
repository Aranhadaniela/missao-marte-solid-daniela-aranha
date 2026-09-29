# Missão Marte Unifor — Refatoração SOLID (Exercício 10)

Nome: **Arthur Adcleybson & Daniela Aranha**

Repositório desta atividade individual da disciplina, com a refatoração do
jogo em console "Missão Marte Unifor" aplicando os princípios SOLID sobre o
código do `Exercício 10`.

## Estrutura do repositório

```text
src/
  exercicio10/          -> código original, preservado sem alterações
  solidresolucao/     -> versão refatorada
    Main.java
    model/
    service/
    presentation/
    repository/
docs/
  uml/                  -> diagramas UML (fonte .puml/.mmd + imagem .png)
apostilas-solid/        -> material de apoio sobre cada princípio SOLID
REVISAO-SOLID.md        -> revisão crítica da refatoração
```

## Como compilar

Pré-requisito: JDK 17 ou superior instalado (`java -version` / `javac -version`).

## Como compilar

Pré-requisito: JDK 17 ou superior instalado. Confira com:

```bash
javac -version
java -version
```

Os dois comandos devem mostrar a mesma versão. Rode todos os comandos abaixo na pasta que contém `src`.

### Linux / macOS (bash)

Código original (para comparação):

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/exercicio10/*.java
```

Versão refatorada:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/solidresolucao -name "*.java")
```

### Windows (PowerShell)

Código original (para comparação):

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -encoding UTF-8 -d out src/exercicio10/*.java
```

Versão refatorada:

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src/solidresolucao | ForEach-Object FullName)
```

### Windows (CMD)

Versão refatorada:

```cmd
mkdir out
dir /s /b src\solidresolucao\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
del fontes.txt
```

> O comando `find` só funciona em bash. No Windows ele é outro programa e causa o erro
> `Arquivo não encontrado - *.java`. Use as versões de PowerShell ou CMD acima.
> A opção `-encoding UTF-8` evita erro de `unmappable character` por causa dos acentos.

## Como executar

Os comandos são iguais em qualquer sistema, na mesma pasta em que você compilou.

Código original:

```bash
java -cp out exercicio10.Main
```

Versão refatorada:

```bash
java -cp out solidresolucao.Main
```

> Se você compilou as duas versões, use uma pasta `out` diferente para cada uma
> (por exemplo `out-original` e `out-solid`) ou apague `out` antes de trocar.

O jogo é o mesmo em ambas as versões: menu inicial, escolha de piloto,
dificuldade e tamanho do mapa, movimentação com `w/a/s/d`, embarque de
passageiros com `c`, saída com `q`, consulta de ranking e opção de resetar o
histórico. O arquivo de ranking da versão refatorada é
`ranking-solid-exercicio10.txt`, separado do arquivo do jogo original
(`ranking.json`), para não misturar os dois durante a comparação.

## O que foi alterado

O código do `Exercício 10` (uma única classe `Main` concentrando menu, regra
de jogo, geração do mapa, desenho em texto e leitura/gravação do ranking) foi
reorganizado em quatro camadas, seguindo o tutorial da disciplina
(`src/README.md`):

- **`Main`** — só monta as dependências (`RankingRepository`, `JogoService`) e
  inicia o jogo. Não tem nenhuma regra de negócio.
- **`service.JogoService`** — dono do fluxo da partida: menu, loop de
  comandos, condição de vitória/derrota, cálculo de pontuação e geração da
  missão (posições de passageiros, asteroides e inimigos).
- **`presentation.MapaRenderer`** — só desenha o estado da missão no console.
  Não decide pontuação nem regra de embarque.
- **`repository`** — `RankingRepository` é o contrato de persistência do
  ranking; `RankingService` é a implementação que grava em arquivo de texto;
  `RankingEntry` é o registro de dados de uma posição do ranking.
- **`model`** — entidades do domínio (`Nave`, `Missao`, `Passageiro` e as
  subclasses `Professor`/`Engenheiro`/`Astronauta`, `Asteroide`, `Inimigo`,
  `Dificuldade`), além das interfaces `Posicionavel` e `Movel`.

## Decisões de projeto (e onde cada princípio SOLID aparece)

- **SRP** — cada classe passou a ter um único motivo para mudar: regra de
  jogo (`JogoService`), desenho do mapa (`MapaRenderer`), persistência
  (`RankingService`) e entidades do domínio (pacote `model`). Antes, tudo
  isso vivia dentro de `Main`.
- **OCP** — novos tipos de passageiro só exigem criar uma subclasse de
  `Passageiro` e implementar `getPontuacao()`/`getSimbolo()`; nem
  `MapaRenderer` nem a regra de embarque em `Missao` precisam ser alterados
  (diferente da versão original, que usava `instanceof` para descobrir o
  símbolo de cada tipo).
- **LSP** — `Professor`, `Engenheiro` e `Astronauta` podem substituir
  `Passageiro` em qualquer lugar do código (embarque, pontuação, desenho)
  sem restringir pré-condições nem lançar exceções que a classe base não
  previa.
- **ISP** — `Posicionavel` e `Movel` separam "ter posição" de "poder se
  mover": um `Asteroide` implementa só a primeira, um `Inimigo` implementa as
  duas. `RankingRepository` tem só os quatro métodos que o `JogoService`
  realmente usa.
- **DIP** — `JogoService` depende da abstração `RankingRepository`, nunca da
  classe concreta `RankingService`; quem decide qual implementação usar é a
  composição feita em `Main`.

Duas pequenas divergências em relação ao tutorial de referência estão
detalhadas e justificadas no `REVISAO-SOLID.md` (uso de `getSimbolo()` na
apresentação em vez de comparação de string, e a extração de métodos menores
dentro de `JogoService.jogarPartida`).

## Limitações conhecidas

- `JogoService` ainda instancia `Random` e `MapaRenderer` diretamente no
  construtor, em vez de recebê-los por injeção — isso dificulta testes
  automatizados determinísticos (detalhado no `REVISAO-SOLID.md`, item de
  prioridade alta).
- `RankingService` grava o ranking em texto separado por `|`; um nome de
  piloto contendo esse caractere quebra a leitura (bug conhecido, registrado
  na revisão).
- Não há testes automatizados (JUnit) versionados neste repositório; a
  validação foi feita jogando manualmente e com um programa avulso de apoio
  durante o desenvolvimento (ver seção de testes do `REVISAO-SOLID.md`).
- A capacidade da nave é igual ao total de passageiros da dificuldade
  escolhida, então a nave nunca fica cheia antes de resgatar todos — ponto
  discutido na seção "Decisões com as quais não concordo" do
  `REVISAO-SOLID.md`.

## Diagramas UML

Na pasta [`docs/uml/`](docs/uml/) estão os dois diagramas pedidos, cada um com
o arquivo-fonte (`.puml`, renderizável em PlantUML, e `.mmd`, renderizável em
Mermaid) e a imagem já gerada.

### Diagrama de classes do domínio

[`docs/uml/diagrama-classes-model.puml`](docs/uml/diagrama-classes-model.puml) · [`docs/uml/diagrama-classes-model.mmd`](docs/uml/diagrama-classes-model.mmd)

![Diagrama de classes do domínio](docs/uml/diagrama-classes-model.png)

Mostra as entidades de `solidexercicio10.model`: a hierarquia
`EntidadeMapa` → `Passageiro` → `Professor`/`Engenheiro`/`Astronauta`, a
realização das interfaces `Posicionavel` e `Movel`, e as associações da
`Missao` com `Nave` (composição 1–1) e com as listas de passageiros,
asteroides e inimigos (agregações 1–0..*).

### Diagrama de pacotes

[`docs/uml/diagrama-pacotes.puml`](docs/uml/diagrama-pacotes.puml) · [`docs/uml/diagrama-pacotes.mmd`](docs/uml/diagrama-pacotes.mmd)

![Diagrama de pacotes](docs/uml/diagrama-pacotes.png)

Mostra a direção das dependências entre `solidexercicio10`,
`service`, `model`, `presentation` e `repository`. O ponto central é que
`service` depende apenas da interface `RankingRepository`; a implementação
`RankingService` fica isolada no pacote `repository`, e é `Main` quem liga as
duas pontas — a aplicação prática do DIP na organização dos pacotes.

## Revisão crítica

A revisão completa (observações por princípio, melhorias identificadas,
prioridades, decisões com as quais concordo/discordo e os testes realizados)
está em [`REVISAO-SOLID.md`](REVISAO-SOLID.md).
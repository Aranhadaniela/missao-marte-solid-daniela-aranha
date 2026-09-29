package solidresolucao.service;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidresolucao.model.Asteroide;
import solidresolucao.model.Dificuldade;
import solidresolucao.model.Engenheiro;
import solidresolucao.model.Inimigo;
import solidresolucao.model.Missao;
import solidresolucao.model.Nave;
import solidresolucao.model.Passageiro;
import solidresolucao.model.Professor;
import solidresolucao.presentation.MapaRenderer;
import solidresolucao.repository.RankingEntry;
import solidresolucao.repository.RankingRepository;

public class JogoService {

    private final RankingRepository rankingRepository;
    private final MapaRenderer mapaRenderer;
    private final Random random;

    public JogoService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
        this.mapaRenderer = new MapaRenderer();
        this.random = new Random();
    }

  
    public void executarLoop(Scanner scanner) {
        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            String opcao = lerLinha(scanner, "Escolha uma opção: ", "1").trim();
            switch (opcao) {
                case "1":
                    jogarPartida(scanner);
                    break;
                case "2":
                    exibirRanking();
                    break;
                case "3":
                    rankingRepository.limpar();
                    System.out.println("Histórico de ranking removido.");
                    break;
                case "4":
                    rodando = false;
                    System.out.println("\nObrigado por jogar a Missão Marte Unifor!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Ranking");
        System.out.println("4. Sair");
        System.out.println("----------------------");
    }

    private void jogarPartida(Scanner scanner) {
        String pilotoNome = lerLinha(scanner, "\nDigite o nome do piloto: ", "Piloto Anônimo").trim();
        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = lerDificuldade(scanner);
        int tamanhoMapa = lerTamanhoMapa(scanner);
        int minX = -tamanhoMapa;
        int maxX = tamanhoMapa;
        int minY = -tamanhoMapa;
        int maxY = tamanhoMapa;

        System.out.println("\nIniciando missão na dificuldade " + dificuldade + "...");
        lerLinha(scanner, "Pressione Enter para decolar!", "");

        Missao missao = criarNovaMissao(dificuldade, minX, maxX, minY, maxY);
        Nave nave = missao.getNave();
        int score = definirPontuacaoInicial(dificuldade);
        int movimentos = 0;
        boolean partidaAtiva = true;
        long tempoInicio = System.currentTimeMillis();

        while (partidaAtiva) {
            mapaRenderer.desenhar(missao, score, pilotoNome, minX, maxX, minY, maxY);

            int passageirosABordo = nave.getPassageiros().size();
            int passageirosRestantes = missao.getPassageiros().size();
            System.out.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes no mapa: %d%n",
                    nave.getX(), nave.getY(), score, nave.getVidas(), passageirosABordo,
                    nave.getCapacidade(), passageirosRestantes);

            String entrada = lerLinha(scanner, "Comando (w/s/a/d/c/q): ", "").trim().toLowerCase();
            if (entrada.isEmpty()) {
                continue;
            }

            char cmd = entrada.charAt(0);
            if (cmd == 'q') {
                System.out.println("Missão abortada pelo piloto.");
                partidaAtiva = false;
            } else if (cmd == 'c') {
                score += embarcarPassageiro(missao);
            } else if (cmd == 'w' || cmd == 's' || cmd == 'a' || cmd == 'd') {
                nave.moverComLimites(cmd, minX, maxX, minY, maxY);
                score--;
                movimentos++;
            } else {
                System.out.println("Comando inválido.");
            }

            missao.moverInimigos();

            if (missao.verificaColisao()) {
                nave.perderVida();
                if (nave.getVidas() > 0) {
                    System.out.printf("Alerta! Colisão detectada! Vidas restantes: %d%n", nave.getVidas());
                } else {
                    System.out.println("GAME OVER! A nave foi destruída.");
                    partidaAtiva = false;
                }
            }

            if (score <= 0 && partidaAtiva) {
                System.out.println("Combustível/Pontuação zerada! Missão perdida.");
                partidaAtiva = false;
            }

            if (partidaAtiva && missao.todosEmbarcados()) {
                partidaAtiva = verificarConclusaoDaMissao(
                        nave, pilotoNome, dificuldade, score, movimentos, tempoInicio);
            }
        }
    }

    private int embarcarPassageiro(Missao missao) {
        Passageiro passageiro = missao.passagemNaPosicao();
        if (passageiro == null) {
            System.out.println("Nenhum passageiro nesta posição.");
            return 0;
        }
        boolean embarcou = missao.embarcarPassageiroNaPosicao();
        if (!embarcou) {
            System.out.println("Nave cheia! Não há espaço para mais passageiros.");
            return 0;
        }
        int bonus = passageiro.getPontuacao();
        System.out.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n", passageiro.getNome(), bonus);
        return bonus;
    }

    private boolean verificarConclusaoDaMissao(Nave nave, String pilotoNome, Dificuldade dificuldade,
                                                int score, int movimentos, long tempoInicio) {
        if (nave.getX() != 0 || nave.getY() != 0) {
            System.out.println("✨ ALERTA: Todos os passageiros resgatados! Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.");
            return true;
        }

        long tempoJogoSegundos = (System.currentTimeMillis() - tempoInicio) / 1000;
        System.out.println("\n================================================================");
        System.out.println("🚀 DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0).");
        System.out.println("Retornando à órbita marciana com todos os passageiros. Missão cumprida!");
        System.out.println("================================================================\n");

        exibirEstatisticas(score, movimentos, tempoJogoSegundos, nave.getPassageiros().size());
        rankingRepository.salvar(pilotoNome, score, dificuldade, nave.getPassageiros().size(), tempoJogoSegundos);
        return false;
    }

    private Dificuldade lerDificuldade(Scanner scanner) {
        System.out.print("Escolha a Dificuldade (facil/medio/dificil): ");
        String valor = lerLinha(scanner, "", "medio").trim();
        return Dificuldade.deString(valor);
    }

    private int lerTamanhoMapa(Scanner scanner) {
        try {
            int tamanho = Integer.parseInt(lerLinha(scanner, "Tamanho do mapa (ex: 5): ", "5"));
            return tamanho > 0 ? tamanho : 5;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida, usando tamanho padrão (5).");
            return 5;
        }
    }

    private int definirPontuacaoInicial(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL: return 30;
            case DIFICIL: return 15;
            default: return 20;
        }
    }

    private Missao criarNovaMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY) {
        int qtdPassageiros = 4;
        int qtdAsteroides = 2;
        int qtdInimigos = 2;

        if (dificuldade == Dificuldade.MEDIO) {
            qtdPassageiros = 5;
        } else if (dificuldade == Dificuldade.DIFICIL) {
            qtdPassageiros = 6;
            qtdAsteroides = 3;
            qtdInimigos = 3;
        }

        Nave nave = new Nave("A-1", 0, 0, qtdPassageiros);
        Missao missao = new Missao(nave);

        posicionarPassageiros(missao, qtdPassageiros, minX, maxX, minY, maxY, nave);
        posicionarAsteroides(missao, qtdAsteroides, minX, maxX, minY, maxY, nave);
        posicionarInimigos(missao, qtdInimigos, minX, maxX, minY, maxY, nave);

        return missao;
    }

    private void posicionarPassageiros(Missao missao, int qtdPassageiros,
                                        int minX, int maxX, int minY, int maxY, Nave nave) {
        int indice = 0;
        while (missao.getPassageiros().size() < qtdPassageiros) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
            int x = posicao[0];
            int y = posicao[1];
            if (indice % 3 == 0) {
                missao.adicionarPassageiro(new Professor("Dr. Silva", x, y));
            } else if (indice % 3 == 1) {
                missao.adicionarPassageiro(new Engenheiro("Eng. Rosa", x, y));
            } else {
                missao.adicionarPassageiro(new Professor("Dr. Lima", x, y));
            }
            indice++;
        }
    }

    private void posicionarAsteroides(Missao missao, int qtdAsteroides,
                                       int minX, int maxX, int minY, int maxY, Nave nave) {
        while (missao.getAsteroides().size() < qtdAsteroides) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
            missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
        }
    }

    private void posicionarInimigos(Missao missao, int qtdInimigos,
                                     int minX, int maxX, int minY, int maxY, Nave nave) {
        while (missao.getInimigos().size() < qtdInimigos) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
            missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
        }
    }

    private int[] sortearPosicaoLivre(Missao missao, int minX, int maxX, int minY, int maxY, Nave nave) {
        int tentativasMaximas = Math.max(20, (maxX - minX + 1) * (maxY - minY + 1) * 2);
        for (int tentativa = 0; tentativa < tentativasMaximas; tentativa++) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (!posicaoOcupada(missao, x, y) && !(x == nave.getX() && y == nave.getY())) {
                return new int[] { x, y };
            }
        }
        throw new IllegalStateException("O mapa não possui posições livres suficientes.");
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return true;
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return true;
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void exibirRanking() {
        List<RankingEntry> ranking = rankingRepository.listar();
        if (ranking.isEmpty()) {
            System.out.println("Nenhum registro de ranking ainda.");
            return;
        }
        System.out.println("\n=== TOP 5 DO RANKING ===");
        for (int i = 0; i < Math.min(5, ranking.size()); i++) {
            RankingEntry entry = ranking.get(i);
            System.out.printf("%d. %s | Pontos: %d | Dif.: %s | Passageiros: %d | %s | Tempo: %ds%n",
                    i + 1, entry.name, entry.score, entry.dificuldade,
                    entry.passageirosColetados, entry.dataHora, entry.tempoJogo);
        }
    }

    private void exibirEstatisticas(int score, int movimentos, long tempoJogoSegundos, int passageirosColetados) {
        System.out.println("=== ESTATÍSTICAS DA MISSÃO ===");
        System.out.printf("Pontuação final: %d%n", score);
        System.out.printf("Movimentos realizados: %d%n", movimentos);
        System.out.printf("Tempo de missão: %d segundos%n", tempoJogoSegundos);
        System.out.printf("Passageiros resgatados: %d%n", passageirosColetados);
    }

    private String lerLinha(Scanner scanner, String mensagem, String valorPadrao) {
        if (!mensagem.isEmpty()) {
            System.out.print(mensagem);
        }
        if (!scanner.hasNextLine()) {
            return valorPadrao;
        }
        String entrada = scanner.nextLine();
        if (entrada == null || entrada.isBlank()) {
            return valorPadrao;
        }
        return entrada;
    }
}
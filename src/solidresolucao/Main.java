package solidresolucao;

import java.util.Scanner;
import solidresolucao.repository.RankingRepository;
import solidresolucao.repository.RankingService;
import solidresolucao.service.JogoService;

public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("             MISSÃO MARTE UNIFOR - VERSÃO SOLID                  ");
        System.out.println("================================================================");
        System.out.println("  Pilote sua nave, salve os passageiros e desvie dos perigos!   ");
        System.out.println("================================================================\n");

        RankingRepository repository = new RankingService("ranking-solid-exercicio10.txt");
        JogoService jogoService = new JogoService(repository);

        try (Scanner scanner = new Scanner(System.in)) {
            jogoService.executarLoop(scanner);
        }
    }
}

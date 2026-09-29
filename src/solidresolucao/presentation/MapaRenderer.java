package solidresolucao.presentation;

import solidresolucao.model.Asteroide;
import solidresolucao.model.Inimigo;
import solidresolucao.model.Missao;
import solidresolucao.model.Passageiro;


public class MapaRenderer {

    public void desenhar(Missao missao, int score, String pilotoNome,
                          int minX, int maxX, int minY, int maxY) {
        System.out.println();
        System.out.printf("Mapa da Missão | Pontos: %d | Piloto: %s%n", score, pilotoNome);
        imprimirCabecalho(minX, maxX);

        for (int y = maxY; y >= minY; y--) {
            System.out.printf("%3d|", y);
            for (int x = minX; x <= maxX; x++) {
                System.out.printf(" %2c", simboloDaCelula(missao, x, y));
            }
            System.out.println();
        }

        System.out.println("Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, A=Asteroide, X=Inimigo, .=Vazio");
        System.out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
    }

    private void imprimirCabecalho(int minX, int maxX) {
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.printf(" %2d", x);
        }
        System.out.println();
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.print(" __");
        }
        System.out.println();
    }

    private char simboloDaCelula(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return '@';
        }
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return passageiro.getSimbolo().charAt(0);
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return asteroide.getSimbolo().charAt(0);
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return inimigo.getSimbolo().charAt(0);
            }
        }
        if (x == 0 && y == 0) {
            return 'L';
        }
        return '.';
    }
}

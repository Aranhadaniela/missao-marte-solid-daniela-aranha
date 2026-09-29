package solidresolucao.model;

import java.text.Normalizer;

public enum Dificuldade {
    FACIL,
    MEDIO,
    DIFICIL;

    public static Dificuldade deString(String valor) {
        if (valor == null) {
            return MEDIO;
        }
        String texto = Normalizer.normalize(valor.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if (texto.startsWith("f")) {
            return FACIL;
        }
        if (texto.startsWith("d")) {
            return DIFICIL;
        }
        return MEDIO;
    }

@Override
public String toString() {
    switch (this) {
        case FACIL: return "Fácil";
        case DIFICIL: return "Difícil";
        default: return "Médio";
    }
}
}
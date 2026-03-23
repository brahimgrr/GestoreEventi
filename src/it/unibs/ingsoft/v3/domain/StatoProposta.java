package it.unibs.ingsoft.v3.domain;

/**
 * State machine for proposals.
 *
 * <pre>
 *   BOZZA ──→ VALIDA ──→ APERTA ──→ CONFERMATA ──→ CONCLUSA
 *                          │
 *                          └──→ ANNULLATA
 * </pre>
 */
public enum StatoProposta
{
    BOZZA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return next == VALIDA;
        }
    },
    VALIDA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return next == APERTA || next == BOZZA;
        }
    },
    APERTA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return next == CONFERMATA || next == ANNULLATA;
        }
    },
    CONFERMATA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return next == CONCLUSA;
        }
    },
    ANNULLATA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    },
    CONCLUSA {
        @Override public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(StatoProposta next);
}

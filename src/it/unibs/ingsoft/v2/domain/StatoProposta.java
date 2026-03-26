package it.unibs.ingsoft.v2.domain;

/**
 * State machine for proposals.
 *
 * <pre>
 *   BOZZA ──→ VALIDA ──→ APERTA (terminal)
 * </pre>
 */
public enum StatoProposta {
    BOZZA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return next == VALIDA;
        }
    },
    VALIDA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return next == APERTA || next == BOZZA;
        }
    },
    APERTA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(StatoProposta next);
}

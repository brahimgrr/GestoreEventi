package it.unibs.ingsoft.v5.domain;

/**
 * State machine for proposals.
 *
 * <pre>
 *   BOZZA ──→ VALIDA ──→ APERTA ──→ CONFERMATA ──→ CONCLUSA
 *                          │  ↘          │  ↘
 *                          │  ANNULLATA  │  RITIRATA
 *                          ↓
 *                        RITIRATA
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
            return next == CONFERMATA || next == ANNULLATA || next == RITIRATA;
        }
    },
    CONFERMATA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return next == CONCLUSA || next == RITIRATA;
        }
    },
    ANNULLATA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    },
    CONCLUSA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    },
    RITIRATA {
        @Override
        public boolean canTransitionTo(StatoProposta next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(StatoProposta next);
}

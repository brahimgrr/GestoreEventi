package it.unibs.ingsoft.v2.model;

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
            return false;
        }
    };

    public abstract boolean canTransitionTo(StatoProposta next);
}

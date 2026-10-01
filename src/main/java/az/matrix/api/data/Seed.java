package az.matrix.api.data;

public final class Seed {

    private static Seed instance;
    private final String sortPrefix;
    private final long alphaId;
    private final long betaId;
    private final long gammaId;
    private final String salimName;
    private final long salimId;

    private Seed() {
        this.sortPrefix = NameFactory.unique("QAAPS-SRT-");
        this.alphaId = Trainings.create(sortPrefix + "-a", "alpha təsvir");
        this.betaId = Trainings.create(sortPrefix + "-B", "Beta təsvir");
        this.gammaId = Trainings.create(sortPrefix + "-c", null);
        this.salimName = NameFactory.unique("QAAPS-Salim-");
        this.salimId = Trainings.create(salimName, "Salim təsviri");
    }

    public static synchronized Seed get() {
        if (instance == null) {
            instance = new Seed();
        }
        return instance;
    }

    public String sortPrefix() {
        return sortPrefix;
    }

    public long alphaId() {
        return alphaId;
    }

    public long betaId() {
        return betaId;
    }

    public long gammaId() {
        return gammaId;
    }

    public String salimName() {
        return salimName;
    }

    public long salimId() {
        return salimId;
    }
}

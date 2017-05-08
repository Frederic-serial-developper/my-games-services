package fdi.games.services.business;

public interface Mapper<Source, Target> {

	Target map(Source source);

}

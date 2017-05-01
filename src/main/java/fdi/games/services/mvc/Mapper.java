package fdi.games.services.mvc;

public interface Mapper<Source, Target> {

	Target map(Source source);

}

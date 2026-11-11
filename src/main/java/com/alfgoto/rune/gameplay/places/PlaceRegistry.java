package com.alfgoto.rune.gameplay.places;

import com.alfgoto.rune.foundations.registry.Registry;
import java.util.Collection;

/** All known Places. Shared as a service; content modules fill it (ADR 0004). */
public final class PlaceRegistry {

    private final Registry<Place> places = new Registry<>("places");

    public Place register(Place place) {
        return places.register(place);
    }

    /** All Places, in registration order. Read it when you use it, not at enable time. */
    public Collection<Place> all() {
        return places.all();
    }
}

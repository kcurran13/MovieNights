import React, { useContext } from 'react';
import { StoreContext } from '../context/Store';
import MovieInformation from './MovieInformation';
import MovieSelector from './MovieSelector';

const SearchMovies = (props) => {
  const store = useContext(StoreContext);

  let handleInputChange = async (e) => {
    e.persist();

    if (e.key === 'Enter') {
      let res = await store.getMoviesFromApi(e.target.value, store.getJwt());
      store.setCurrentSearchResult(res.Search);
    }
  };

  function displayMovies() {
    if (props.movieSelector) {
      return <div> {store.currentSearchResult.map((movie) => <MovieSelector callback={props.callback} key={movie.imdbID} {...movie} />)}</div>
    } else {
      return store.currentSearchResult.map((movie) => <MovieInformation className="col" key={movie.imdbID} {...movie} />)
    }
  }

  return (
    <div>
      <label>Search Movies</label>
      <input autoFocus onKeyPress={(e) => handleInputChange(e)} type="text" onChange={(e) => handleInputChange(e)} placeholder="Search..." />
      <div className="row m-2">
        {store.currentSearchResult === undefined || store.currentSearchResult === null ? null : displayMovies()}
      </div>
    </div>
  )
};

export default SearchMovies;
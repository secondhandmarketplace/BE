import React from "react";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMagnifyingGlass } from "@fortawesome/free-solid-svg-icons"; // Search icon
import "./header.css";

const Header = () => {
  return (
    <div className="header-container">
      <Link to="/home">
        <h1>KDT</h1>
      </Link>
      <div className="header-search">
        <Link to="/search">
          <button className="search-button">
            <FontAwesomeIcon icon={faMagnifyingGlass} />
          </button>
        </Link>
      </div>
    </div>
  );
};

export default Header;

import React, { Component } from 'react'
import { Link } from "react-router-dom";

export default class OLink extends Component {
  render() {
    return (
      <Link style={{ textDecoration: 'none' }} {...this.props} />
    )
  }
}

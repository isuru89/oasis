import React, { Component } from 'react'
import { Link } from "react-router-dom";

export default class OLink extends Component {
  render() {
    const { style = {} } = this.props;
    return (
      <Link style={{ textDecoration: 'none', ...style }} {...this.props} />
    )
  }
}

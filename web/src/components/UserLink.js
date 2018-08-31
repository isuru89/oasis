import React, { Component } from 'react'
import styled from 'styled-components';
import OLink from "./OLink";

const Span = styled.span`
  font-weight: 500;
  color: hsl(203, 80%, 30%);
`

export default class UserLink extends Component {
  render() {
    const { user, uid } = this.props;
    if (uid) {
      return <Span><OLink to={`profile/${uid}`} style={{ color: 'hsl(203, 80%, 30%)' }}>{user}</OLink></Span>
    } else {
      return <Span>{user}</Span>;
    }
    
  }
}

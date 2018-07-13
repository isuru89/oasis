import React, { Component } from 'react'
import styled from 'styled-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import { formatInt } from '../../utils'

const Wrapper = styled.div`
  line-height: 2;
  display: flex;
  opacity: ${props => props.active ? 1 : 0.7};
  padding: 0 10px;
  
  font-weight: ${props => props.active ? 'bold' : 'lighter'};
`

const Indicator = styled.div`
  min-width: 20px;
`

const TeamNameSpan = styled.div`
  width: 80%;
  flex: 1;
`

const StatCell = styled.div`
  width: 80px;
  text-align: right;
`

export default class TeamHistoryRecord extends Component {
  render() {
    const { team, points, badges, thropies, since, until, active = false } = this.props;

    return (
      <Wrapper active={active}>
        <Indicator />
        <TeamNameSpan>{team}</TeamNameSpan>
        <StatCell>{formatInt(points)} <FontAwesomeIcon icon="coins" /></StatCell>
        <StatCell>{formatInt(badges)} <FontAwesomeIcon icon="award" /></StatCell>
        <StatCell>{formatInt(thropies)} <FontAwesomeIcon icon="trophy" /></StatCell>
      </Wrapper>
    )
  }
}

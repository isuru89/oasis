import React, { Component } from 'react'
import badge from '../badge.png'
import styled from 'styled-components';
import Badge from './Badge';

const Wrapper = styled.div`
  display: flex;
  flex-wrap: wrap;
`

export default class BadgeView extends Component {
  render() {
    return (
      <Wrapper>
        <Badge image={badge} imageOnly={false} title="Superstar" description="Get 3 golds in one week."
          achievedDate="2018" />
        <Badge image={badge} />
        <Badge image={badge} />
        <Badge image={badge} acquired={false} />
        <Badge image={badge} />
        <Badge image={badge} />
        <Badge image={badge} />
        <Badge image={badge} />
        <Badge image={badge} />
        <Badge image={badge} />
      </Wrapper>
    )
  }
}

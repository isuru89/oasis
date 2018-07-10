import React, { Component } from 'react'
import styled from 'styled-components'

const Image = styled.img`
  border: 5px solid #ffffff22;
  border-radius: 50%;
`

export default class Avatar extends Component {
  render() {
    return (
      <Image src={this.props.image} width={this.props.size} height={this.props.size} />
    )
  }
}

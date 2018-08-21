import React, { Component } from 'react'
import styled from 'styled-components'

const Image = styled.img`
  border: ${props => props.border + 'px solid ' + props.borderColor};
  border-radius: 50%;
`

export default class Avatar extends Component {
  render() {
    const { image, size, style, border = 5, borderColor = '#ffffff22' } = this.props;
    return (
      <Image src={this.props.image} 
        width={this.props.size} 
        height={this.props.size} 
        style={this.props.style || {}}
        border={border} 
        borderColor={borderColor} />
    )
  }
}

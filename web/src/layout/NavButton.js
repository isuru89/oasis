import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  width: 120px;
  height: 100px;
  text-align: center;
  font-size: 0.9em;
  color: ${props => props.selected ? '#21BFE7' : '#21BFE788'};
  user-select: none;
  box-sizing: border-box;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  border-bottom: 1px solid #ffffff22;
  background-color: ${props => props.selected ? '#1E2B34' : '#00000000'};
  border-left: ${props => props.selected ? '5px solid #24D5E9' : '5px solid #ffffff00'};
  border-right: 5px solid #ffffff00;

  &:hover {
    background-color: #1E2B3488;
    color: #21BFE7;
  }
`

const Image = styled.div`
  width: 60px;
  height: 60px;
  font-size: 40px;
  text-align: center;
`

const Label = styled.div`
  font-size: 12px;
  letter-spacing: 0.2px;
  text-transform: uppercase;
`

export default class NavButton extends Component {
  render() {
    return (
      <Wrapper {...this.props} onClick={this._whenClicked}>
        <Image>
          {this.props.image}
        </Image>
        <Label>{this.props.label}</Label>
      </Wrapper>
    )
  }

  _whenClicked = () => {
    if (this.props.onClick) {
      this.props.onClick(this.props.id);
    }
  }
}

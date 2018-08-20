import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  width: 120px;
  height: 100px;
  text-align: center;
  font-size: 0.9em;
  color: ${props => props.selected ? 'hsl(203, 100%, 70%)' : 'hsl(203, 30%, 50%)'};
  user-select: none;
  box-sizing: border-box;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  border-bottom: 1px solid hsl(203, 40%, 30%);
  background-color: ${props => props.selected ? 'hsl(203, 70%, 10%)' : 'transparent'};
  border-left: ${props => props.selected ? '5px solid hsl(203, 100%, 70%)' : '5px solid transparent'};

  &:hover {
    background-color: ${props => props.selected ? 'hsl(203, 70%, 10%)' : 'hsl(203, 50%, 20%)' };
    color: hsl(203, 100%, 70%);
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

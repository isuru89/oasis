import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  display: flex;
  text-align: left;
  padding: 5px 0;
  align-items: center;
`

const Image = styled.div`
  font-size: ${props => props.imgSize ? props.imgSize + 'px' : '16px'};
  padding: 0 10px;
  line-height: 2;
  text-align: center;
  margin: 5px 10px 5px 0;
  border-radius: 50%;
  color: ${props => props.color};
  background-color: ${props => props.imgColor ? props.imgColor : '#ffffff00'};
`

const TextContent = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
`

const Label = styled.div`
  color: white;
  font-weight: 300;
  opacity: 0.7;
`

const Value = styled.div`
  font-size: 20px;
  letter-spacing: 1.5px;
`

export class ImageContent extends Component {
  render() {
    const { image } = this.props;

    return (
      <Wrapper>
        <Image {...this.props}>{image}</Image>
        <TextContent>
          {this.props.children}
        </TextContent>
      </Wrapper>
    )
  }
}

export default class ImageTitleValue extends Component {
  render() {
    const { title, value, image, color } = this.props;

    return (
      <Wrapper>
        <Image {...this.props}>{image}</Image>
        <TextContent>
          { title && <Label>{title}</Label> }
          { value && <Value>{value}</Value> } 
        </TextContent>
      </Wrapper>
    )
  }
}

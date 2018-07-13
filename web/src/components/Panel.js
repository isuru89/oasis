import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  padding: 5px;
  margin: 2px;
`

const TitleBar = styled.div`
  padding: 5px;
  padding-bottom: 4px;
  border: 2px solid #739CAE;
  border-radius: 5px 5px 0 0;
`

const Content = styled.div`
  padding: 5px;
  border-radius: 0 0 5px 5px;
  border: 1px solid #739CAE;
  border-top: none;
`

export default class Panel extends Component {
  render() {
    return (
      <Wrapper>
        <TitleBar>
          {this.props.title}
        </TitleBar>
        <Content>
          {this.props.children}
        </Content>
      </Wrapper>
    )
  }
}
